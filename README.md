# BFF Pattern

A full-stack implementation of the Backend for Frontend (BFF) pattern, built to the requirements of
[RFC 10017](https://datatracker.ietf.org/doc/html/rfc10017), the IETF's Best Current Practice for OAuth 2.0 in
browser-based apps. A React single-page app signs users in with OAuth 2.0 and OpenID Connect, but the access and refresh
tokens never reach the browser. A Spring Boot BFF keeps them on the server, and the browser authenticates with an
`HttpOnly` session cookie. The only token the browser sees is the ID token, which it passes to the authorization server
at logout.

The project covers the whole OAuth setup, not just the client. The authorization server, the BFF and the resource
server are three separate Spring Boot services, so login isn't handed off to a hosted identity provider. A small
message board is the demo application, so the focus stays on the security architecture.
A [comparison against the RFC](#compared-with-rfc-10017) shows which requirements are met and which aren't yet.

This project is part of my [thesis](https://www.theseus.fi/items/241cb384-f10b-46c3-8ff4-0a3eb0b4cba4) (in Finnish).

**Tech stack**

| Area             | Technologies                                                                                                       |
|------------------|--------------------------------------------------------------------------------------------------------------------|
| Backend          | Java, Spring Boot, Spring Security, Spring Authorization Server, Spring Cloud Gateway Server MVC, Spring Data JDBC |
| Frontend         | React, TypeScript, Vite, TanStack Query                                                                            |
| Data and tooling | PostgreSQL, Docker Compose, Gradle, pnpm                                                                           |
| Testing          | JUnit, Mockito, MockMvc, WebTestClient, Testcontainers, MockWebServer                                              |


## Architecture

```mermaid
flowchart LR
    Browser["Browser<br/>React SPA"]
    BFF["BFF<br/>OAuth client and gateway<br/>holds the tokens"]
    AS["Authorization server<br/>login, consent, JWTs"]
    RS["Resource server<br/>messages REST API"]
    DB[("PostgreSQL")]
    Browser -- " session cookie and CSRF token " --> BFF
    Browser -. " login, consent and logout redirects " .-> AS
    BFF -- " code exchange, token refresh, userinfo " --> AS
    BFF -- " Bearer access token " --> RS
    RS -. " public signing keys (JWKS) " .-> AS
    AS --> DB
    RS --> DB
```

| Service                                        | Port | Responsibility                                                                                                                                         |
|------------------------------------------------|------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| [`spa`](spa)                                   | 5173 | React UI. Holds no access or refresh tokens and calls the BFF with `credentials: "include"`.                                                           |
| [`bff`](bff)                                   | 8080 | Confidential OAuth client and API gateway. Keeps the tokens server-side, relays the access token on proxied requests and refreshes it when it expires. |
| [`authorization-server`](authorization-server) | 9000 | Handles login, consent and logout, and issues signed JWTs with a custom `user_id` claim.                                                               |
| [`resource-server`](resource-server)           | 8090 | Stateless REST API. Validates JWTs, including their issuer and audience, checks scopes and limits each user to their own data.                         |

### Login flow

```mermaid
sequenceDiagram
    actor Browser
    participant BFF
    participant AS as Authorization server
    participant RS as Resource server
    Browser ->> BFF: Login button navigates the page to the BFF
    BFF -->> Browser: Redirect to the authorization endpoint with PKCE challenge, state and nonce
    Browser ->> AS: User signs in and gives consent
    AS -->> Browser: Redirect back to the BFF with an authorization code
    Browser ->> BFF: Authorization code
    BFF ->> AS: Exchange code, client secret and PKCE verifier
    AS -->> BFF: Access, refresh and ID tokens
    BFF -->> Browser: Redirect to the SPA with an HttpOnly session cookie
    Note over Browser, BFF: The tokens stay on the BFF
    Browser ->> BFF: fetch /api/messages with the session cookie
    BFF ->> RS: GET /messages with the Bearer access token
    RS -->> BFF: JSON response
    BFF -->> Browser: JSON response
```

Access tokens live for five minutes. When one expires, the BFF gets a new one with the refresh token, and the SPA
doesn't notice. This works until the refresh token expires, as described in [Session lifetimes](#session-lifetimes).

### Logout flow

Logout has to end two sessions: the BFF session and the authorization server's login session. If only the BFF session
ended, the authorization server would sign the user straight back in without a password. The BFF uses
[OpenID Connect RP-Initiated Logout](https://openid.net/specs/openid-connect-rpinitiated-1_0.html).

```mermaid
sequenceDiagram
    actor Browser
    participant BFF
    participant AS as Authorization server
    Browser ->> BFF: fetch POST /logout with the session cookie and CSRF token
    Note over BFF: Ends the session, which also deletes the tokens
    BFF -->> Browser: 202 with the authorization server's logout URL in the Location header
    Browser ->> AS: Navigate to the logout URL, which includes the ID token
    AS -->> Browser: End the login session and redirect to the SPA
```

The BFF returns the logout URL instead of redirecting to it, because `fetch` can't turn a cross-origin redirect into a
page navigation. The SPA navigates to the URL itself.

Logout doesn't revoke the refresh token with
[OAuth 2.0 Token Revocation](https://datatracker.ietf.org/doc/html/rfc7009). Section 6.1.2.1 of RFC 10017 leaves the
behavior of the logout endpoint to the application. The BFF's copy of the tokens is deleted with the session, and the
authorization server accepts the refresh token until it expires.

### Session lifetimes

| What                               | Lifetime                                   |
|------------------------------------|--------------------------------------------|
| BFF session                        | 30 minutes since the last request          |
| Authorization server login session | 12 hours since the browser last visited it |
| Access token                       | 5 minutes                                  |
| Refresh token                      | 60 minutes from login                      |

Spring Authorization Server reuses the same refresh token on every refresh, so its expiry doesn't move. An active user
stays signed in for about an hour. After that the authorization server rejects the refresh token, and the BFF ends its
session and answers `401`. Section 6.1.2.2 of RFC 10017 suggests this: when the BFF learns that the refresh token is no
longer valid, it makes sense to end the session. The same happens when a restart of the authorization server loses the
in-memory authorizations. The BFF session isn't given a fixed lifetime equal to the refresh token's, which section
6.1.2.2 also suggests, because servlet sessions only expire after inactivity. Ending the session when the refresh
token is rejected has the same effect within the lifetime of one access token.

The SPA shows the logged-out view whenever the BFF answers `401`, including after the BFF session has expired. Signing
in again doesn't ask for the password while the authorization server's login session is alive.

Refresh token rotation would extend the sign-in on every refresh. It's left out, because Spring's OAuth client doesn't
serialize refreshes: two parallel requests would send the same refresh token, and the second one would be rejected.

## Compared with RFC 10017

Section 6.1 of the RFC lists requirements for a BFF. This is how the project compares:

| Requirement                            | Level  | Status                                                                                                                          |
|----------------------------------------|--------|---------------------------------------------------------------------------------------------------------------------------------|
| Act as a confidential client           | MUST   | **Met.** Uses `client_secret_basic`.                                                                                            |
| `HttpOnly` cookies                     | MUST   | **Met for the session cookie.** The CSRF cookie has to stay readable so that the SPA can send it back. It holds no credentials. |
| Implement a CSRF defence               | MUST   | **Met.** CSRF token and restrictive CORS.                                                                                       |
| `Secure` cookies                       | MUST   | **Not met in this setup.** Local development runs on plain HTTP.                                                                |
| Require a custom request header        | SHOULD | **Met** for state-changing requests.                                                                                            |
| `SameSite=Strict`                      | SHOULD | **Met for the session cookie.** This assumes the authorization server is on the same site as the BFF.                           |
| Cookie `Path=/`, no `Domain` attribute | SHOULD | **Met.**                                                                                                                        |
| `__Host-Http-` cookie prefix           | SHOULD | **Not met.** It requires HTTPS.                                                                                                 |

## Running locally

**Prerequisites:** JDK 21, Docker, Node.js 20.19 or later, and pnpm.

Each service runs in its own terminal. Start them in this order, because the BFF reads the authorization server's
configuration at startup. Spring Boot's Docker Compose support starts PostgreSQL from [`compose.yaml`](compose.yaml)
automatically.

```bash
# 1. Authorization server, port 9000
cd authorization-server && ./gradlew bootRun

# 2. Resource server, port 8090
cd resource-server && ./gradlew bootRun

# 3. BFF, port 8080
cd bff && ./gradlew bootRun

# 4. SPA, port 5173
cd spa && pnpm install && pnpm dev
```

Open http://localhost:5173 and sign in as `user` with the password `password`. Use `localhost`, not `127.0.0.1`.
CORS only allows `http://localhost:5173`, and the cookies depend on every service being on the same site.

### Running the tests

```bash
cd <service> && ./gradlew test
```

The authorization server and resource server tests use Testcontainers, so Docker has to be running. The BFF's
`BffApplicationTests` needs the authorization server running, because it reads the authorization server's configuration
at startup. The other BFF tests don't.

## Known limitations

This is a local development setup. Before production at least the following should be changed:

- **HTTPS and cookies:** Serve everything over HTTPS, then enable `Secure` and the `__Host-Http-` prefix to meet the
  remaining cookie requirements.
- **Secrets:** The client secret is in `application.yaml` and the demo password uses `{noop}`. These belong in a secret
  store and a real password hash.
- **In-memory state:** Sessions, OAuth authorizations and signing keys live in memory, so a restart signs users out and
  the services can't scale horizontally. Spring Session and persistent authorization storage and keys would fix this.
- **Configuration:** URLs are hard-coded for localhost. Production would use external configuration, and would serve
  the SPA from the same origin as the BFF so that CORS isn't needed.

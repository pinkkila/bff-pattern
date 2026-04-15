export type TUser = {
  username: string;
}

export type TMessage = {
  id: number;
  content: string;
};

export type TPage = {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
};

export type TMessagesPage = {
  content: TMessage[];
  page: TPage;
};

export type TMessageRequest = {
  content: string;
};

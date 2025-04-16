import { atom } from "recoil";

export const userInfoState = atom({
  key: "userInfo",
  default: {
    id: null,
    name: null,
    password: null,
    roles: [],
  },
});

export const themeState = atom({
  key: "themeState",
  default: localStorage.getItem("custom-theme") ?? "Dark",
});

export const alertState = atom({
  key: "alertState",
  default: { open: false, type: undefined, message: "" },
});

export const selectedFundState = atom({
  key: "selectedFundState",
  default: null,
});

export const selectedCategoryTypeState = atom({
  key: "selectedCategoryTypeState",
  default: null,
});

export const selectedCategoryState = atom({
  key: "selectedCategoryState",
  default: null,
});

export const fundsOfInvestorCategoryState = atom({
  key: "fundsOfInvestorCategoryState",
  default: [],
});

export const selectedInvestorForCategoryState = atom({
  key: "selectedInvestorForCategoryState",
  default: null,
});

export const selectedInvestorState = atom({
  key: "selectedInvestorState",
  default: null,
});

export const searchFormState = atom({
  key: "searchFormState",
  default: null,
});

export const fundsIdState = atom({
  key: "fundsIdState",
  default: [],
});

export const rtValuesState = atom({
  key: "rtValuesState",
  default: [],
});

export const refreshInvestorState = atom({
  key: "refreshInvestorState",
  default: 0,
});

export const refreshFundState = atom({
  key: "refreshFundState",
  default: 0,
});

export const refreshSummaryState = atom({
    key: "refreshSummaryState",
    default: 0,
  });

  export const autoRefreshRtvaluesState = atom({
    key: "autoRefreshRtvaluesState",
    default: 0,
  });

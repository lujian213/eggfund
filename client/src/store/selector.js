import { selector } from "recoil";
import axios from "axios";
import {
  autoRefreshRtvaluesState,
  fundsIdState,
  refreshFundState,
  refreshInvestorState,
  refreshSummaryState,
  searchFormState,
  selectedFundState,
  selectedInvestorState,
} from "./atom";
import { BASE_URL } from "../utils/get-baseurl";

export const fundsQuery = selector({
  key: "fundsQuery",
  get: async ({ get }) => {
    get(refreshFundState);
    try {
      const response = await axios.get(`${BASE_URL}/funds`);
      const funds = response.data || [];
      return funds;
    } catch (e) {
      return [];
    }
  },
});

export const investorsQuery = selector({
  key: "investorsQuery",
  get: async ({ get }) => {
    get(refreshInvestorState);
    try {
      const response = await axios.get(`${BASE_URL}/investors`);
      const investors = response.data || [];
      return investors;
    } catch (e) {
      return [];
    }
  },
});

export const fundTypesState = selector({
  key: "fundTypesState",
  get: async ({ get }) => {
    try {
      const response = await axios.get(`${BASE_URL}/fundtypes`);
      const result = response.data || [];
      return result;
    } catch (e) {
      return [];
    }
  },
});

export const investorsForFundQuery = selector({
  key: "investorsForFundQuery",
  get: async ({ get }) => {
    const selectedFund = get(selectedFundState);
    if (!selectedFund) return [];
    try {
      const response = await axios.get(`${BASE_URL}/investors/${selectedFund}`);
      const investors = response.data || [];
      return investors;
    } catch (e) {
      return [];
    }
  },
  cachePolicy_UNSTABLE: {
    eviction: "most-recent",
  },
});

export const valuesQuery = selector({
  key: "valuesQuery",
  get: async ({ get }) => {
    try {
      const response = await axios.get(`${BASE_URL}/values`);
      const values = response.data || [];
      return values;
    } catch (e) {
      return [];
    }
  },
});

export const investsQuery = selector({
  key: "investsQuery",
  get: async ({ get }) => {
    const selectedFund = get(selectedFundState);
    const selectedInvestor = get(selectedInvestorState);
    const params = get(searchFormState);
    const paramsWithoutEmptyValue = Object.keys(params || {}).reduce(
      (acc, key) => {
        if (params[key] !== "") {
          acc[key] = params[key];
        }
        return acc;
      },
      {}
    );
    if (selectedFund === null || selectedInvestor === null) return [];
    try {
      const response = await axios.get(
        `${BASE_URL}/invests/${selectedInvestor}/${selectedFund}`,
        null,
        {
          params: paramsWithoutEmptyValue,
        }
      );
      const invests = response.data || [];
      return invests;
    } catch (e) {
      return [];
    }
  },
});

export const summaryQuery = selector({
  key: "summaryQuery",
  get: async ({ get }) => {
    const selectedFund = get(selectedFundState);
    const selectedInvestor = get(selectedInvestorState);
    const params = get(searchFormState);
    get(refreshSummaryState);
    const paramsWithoutEmptyValue = Object.keys(params || {}).reduce(
      (acc, key) => {
        if (params[key] !== "") {
          acc[key] = params[key];
        }
        return acc;
      },
      {}
    );
    if (!selectedFund || !selectedInvestor) return null;
    try {
      const response = await axios.post(
        `${BASE_URL}/summary/${selectedInvestor}/${selectedFund}`,
        null,
        {
          params: paramsWithoutEmptyValue,
        }
      );
      const summary = response.data || null;
      return summary;
    } catch (error) {
      return null;
    }
  },
});

export const rtValuesQuery = selector({
  key: "rtValuesrtValuesQuery",
  get: async ({ get }) => {
    const selectedFunds = get(fundsIdState);
    get(autoRefreshRtvaluesState);
    if (!selectedFunds || selectedFunds.length === 0) return [];
    try {
      const response = await axios.get(`${BASE_URL}/rtvalues`, {
        params: {
          codes: selectedFunds.join(","),
        },
      });
      const result = response.data || {};
      let datasource = [];
      Object.keys(result).forEach((key) => {
        datasource = [...datasource, { code: key, ...result[key] }];
      });
      return datasource;
    } catch (e) {
      return [];
    }
  },
});

import { selector } from "recoil";
import { refreshInvestsState, searchFormState, selectedFundState, selectedInvestorState } from "./atom";
import axios from "axios";

export const investorsForFundQuery = selector({
    key: 'settingsinvestorsForFundQuery',
    get: async ({get}) => {
        const selectedFund = get(selectedFundState);
        if(selectedFund === null) return [];
        const response = await axios.get(`/investors/${selectedFund}`);
        const investors = response.data || [];
        return investors
    }
})

export const investsQuery = selector({
    key: 'settingsinvestsQuery',
    get: async ({get}) => {
        const selectedFund = get(selectedFundState);
        const selectedInvestor = get(selectedInvestorState);
        get(refreshInvestsState);
        const params = get(searchFormState);
        const paramsWithoutEmptyValue = Object.keys(params || {}).reduce((acc, key) => {
            if(params[key] !== "") {
                acc[key] = params[key];
            }
            return acc
        }, {})
        if(selectedFund === null || selectedInvestor === null) return [];
        const response = await axios.get(`/invests/${selectedInvestor}/${selectedFund}`, {
            params: paramsWithoutEmptyValue
        });
        const invests = response.data || [];
        return invests
    }
})
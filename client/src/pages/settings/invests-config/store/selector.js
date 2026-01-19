import { selector } from "recoil";
import { refreshInvestsState, searchFormState, selectedFundState, selectedInvestorState } from "./atom";
import axios from "axios";
import { BASE_URL } from "../../../../utils/get-baseurl";

export const investorsForFundQuery = selector({
    key: 'settingsinvestorsForFundQuery',
    get: async ({get}) => {
        const selectedFund = get(selectedFundState);
        if(selectedFund === null) return [];
        const response = await axios.get(`${BASE_URL}/investors/${selectedFund}`);
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
        const response = await axios.get(`${BASE_URL}/invests/${selectedInvestor}/${selectedFund}`, {
            params: paramsWithoutEmptyValue
        });
        const invests = response.data || [];
        invests.sort((a, b) => {
            if(a.day === b.day) {
                return b.userIndex - a.userIndex;
            }
            return new Date(b.day) - new Date(a.day);
        });
    return invests;
  },
});
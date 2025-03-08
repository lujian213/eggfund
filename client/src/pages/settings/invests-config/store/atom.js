import {atom} from 'recoil';

export const selectedFundState = atom({
    key: 'investsConfigselectedFundState',
    default: null
})

export const selectedInvestorState = atom({
    key: 'investsConfigselectedInvestorState',
    default: null
})

export const searchFormState = atom({
    key: 'investsConfigsearchFormState',
    default: null
})

export const refreshInvestsState = atom({
    key: 'investsConfigrefreshInvestsState',
    default: 0
})
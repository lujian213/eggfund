import { useEffect } from "react";
import { useSetRecoilState } from "recoil";
import axios from 'axios';
import { alertState } from "../store/atom";


export default function useInterceptor() {
    const setAlert = useSetRecoilState(alertState);

    useEffect(()=> {
        const onRequest = (config) => {
            return config;
        }
        const onRequestError = (error) => {
            return Promise.reject(error);
        }

        const onResponse = (response) => {
            return response;
        }

        const onResponseError = (error) => {
            setAlert({
                open: true,
                type: 'error',
                message: error?.response?.data?.message,
            })
        }

        const setupInterceptorsTo = (axiosInstance) => {
            axiosInstance.interceptors.request.use(onRequest, onRequestError);
            axiosInstance.interceptors.response.use(onResponse, onResponseError);
            return axiosInstance;
        }

        setupInterceptorsTo(axios);

    }, [setAlert]);
}
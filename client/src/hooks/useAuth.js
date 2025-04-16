import { useRecoilState } from "recoil";
import { userInfoState } from "../store/atom";
import { useEffect, useState } from "react";
import axios from "axios";

const BASE_URL = process.env.REACT_APP_BASE_URL;

export default function useAuth() {
  const [userInfo, setUserInfo] = useRecoilState(userInfoState);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    const handleLogin = async () => {
      let response;
      try {
        const instance = axios.create();
        response = await instance.get(`${BASE_URL}/loginUser`);
      } catch (error) {
        console.error("Error fetching user data:", error);
        return;
      }
      if(!response) {
        console.error("Error fetching user data");
        return;
      }
      const user = response.data;
      setUserInfo(user);
    };
    handleLogin();
  }, [setUserInfo]);

  useEffect(() => {
    if (userInfo && userInfo.id) {
      setIsAuthenticated(true);
    } else {
      setIsAuthenticated(false);
    }
  }, [userInfo]);

  return { isAuthenticated };
}

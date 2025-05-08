import { useRecoilState } from "recoil";
import { userInfoState } from "../store/atom";
import { useEffect, useState } from "react";
import axios from "axios";
import { BASE_URL } from "../utils/get-baseurl";

export default function useAuth() {
  const [userInfo, setUserInfo] = useRecoilState(userInfoState);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    const handleLogin = async () => {
      let response;
      try {
        const instance = axios.create();
        const token = localStorage.getItem("EGG-Authorization");
        if (token) {
          instance.defaults.headers.common["authorization"] = `${token}`;
        } else {
          console.error("No token found");
          return;
        }
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

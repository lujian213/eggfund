import { useRecoilState } from "recoil";
import { userInfoState } from "../store/atom";
import { useEffect, useState } from "react";

export default function useAuth() {
  const [userInfo] = useRecoilState(userInfoState);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    if (userInfo && userInfo.id) {
      setIsAuthenticated(true);
    } else {
      setIsAuthenticated(false);
    }
  }, [userInfo]);

  return { isAuthenticated };
}

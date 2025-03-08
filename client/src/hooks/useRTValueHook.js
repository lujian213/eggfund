import {
  useRecoilValue,
  useRecoilValueLoadable,
  useSetRecoilState,
} from "recoil";
import { fundsQuery, rtValuesQuery } from "../store/selector";
import {
  autoRefreshRtvaluesState,
  fundsIdState,
  rtValuesState,
} from "../store/atom";
import { useEffect } from "react";

export default function useRTValueHook() {
  const funds = useRecoilValue(fundsQuery);
  const setFundsId = useSetRecoilState(fundsIdState);
  const { contents, state } = useRecoilValueLoadable(rtValuesQuery);
  const setRtValues = useSetRecoilState(rtValuesState);
  const setAutoRefreshData = useSetRecoilState(autoRefreshRtvaluesState);

  useEffect(() => {
    const interval = setInterval(() => {
      setAutoRefreshData((pre) => pre + 1);
    }, 30 * 1000);
    return () => clearInterval(interval);
  }, [setAutoRefreshData]);

  useEffect(() => {
    funds && setFundsId(funds.map((item) => item.id));
  }, [funds, setFundsId]);

  useEffect(() => {
    if (state !== "hasValue") return;
    setRtValues(contents);
  }, [contents, state, setRtValues]);
}

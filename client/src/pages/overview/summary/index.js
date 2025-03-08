import { useRecoilValue } from "recoil";
import { summaryQuery } from "../../../store/selector";
import { Stack } from "@mui/material";
import TotalDetails from "./total-details";

export default function Summary() {
    const summary = useRecoilValue(summaryQuery);

    return (
        <div style={{flexBasis: '70%'}}>
            <Stack spacing={1} style={{height: '100%'}}>
                <TotalDetails summary={summary} />
            </Stack>
        </div>
    )
}
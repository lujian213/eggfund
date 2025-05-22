import { useRecoilValue } from "recoil";
import CalculateIcon from '@mui/icons-material/Calculate';
import { rtValuesState, selectedFundState } from "../../../../store/atom";
import { useState } from "react";
import {
  Box,
  Card,
  CardContent,
  IconButton,
  Stack,
  Tooltip,
  Typography,
} from "@mui/material";
import {
  formatNumber,
  formatNumberByPercent,
} from "../../../../utils/process-number";
import CalculateShareModal from "./calculate-share-modal";

const itemStyle = {
  padding: 1,
  backgroundColor: (theme) => theme.palette.background.console,
  borderRadius: 1,
  boxShadow: "0 1px 3px rgba(0, 0, 0, 0.5)",
  textAlign: "center",
  flex: 1,
  display: "flex",
  flexDirection: "column",
  justifyContent: "space-between",
  textWrap: "nowrap",
};

export default function TotalDetails(props) {
  const { summary } = props;
  const selectedFund = useRecoilValue(selectedFundState);
  const rtValues = useRecoilValue(rtValuesState);
  const [calculateShareModal, setCalculateShareModal] = useState(false);

  const findRtvalue = rtValues.find((item) => item.code === selectedFund);
  const rtUnitvalue = findRtvalue?.unitValue;

  const {
    totalLongAmt,
    totalShortAmt,
    totallongQuota,
    totalShortQuota,
    netAmt,
    netQuota,
    earning,
    earningRate,
    averagePrice,
    averageUnitValue,
    lastFundDate,
    lastUnitValue,
    predictedValue,
    predictedValueRMB,
    raiseRate,
    estPrice,
    estPriceTable,
    totalFee,
    //totalTax,
    totalDividendAmt,
    fxRateInfo,
  } = summary || {};

  const renderNumberColor = (number) => {
    if (number < 0) {
      return "success.main";
    } else if (number > 0) {
      return "error.main";
    }
    return "text.primary";
  };

  return (
    <Card sx={{ flexGrow: 1 }}>
      <CardContent>
        <Box
          sx={{
            display: "flex",
            gap: 2,
            flexWrap: "wrap",
          }}
        >
          <Box sx={itemStyle}>
            <Typography variant="div">Previous COB</Typography>
            <div>
              <Box>{lastFundDate}</Box>
              <Box>{<Box>{formatNumber(lastUnitValue, 4)}</Box>}</Box>
            </div>
          </Box>
          <Box sx={itemStyle}>
            <Typography variant="div">Raise Rate(%)</Typography>
            <Box>{formatNumberByPercent(raiseRate)}</Box>
          </Box>
          <Box sx={itemStyle}>
            <Typography variant="div">
              Est. Price
              <Tooltip title="Calculate share">
                <IconButton
                  size="small"
                  onClick={() => {
                    setCalculateShareModal(true);
                  }}
                >
                  <CalculateIcon />
                </IconButton>
              </Tooltip>
            </Typography>
            <Box color="warning.main">{formatNumber(estPrice, 4)}</Box>
            <CalculateShareModal
              open={calculateShareModal}
              price={formatNumber(estPrice, 4)}
              handleClose={() => setCalculateShareModal(false)}
            />
          </Box>
          <Box sx={itemStyle}>
            <Typography variant="div">Market Value</Typography>
            <Box>{formatNumber(predictedValueRMB, 2)}(RMB)</Box>
            <Box>{formatNumber(predictedValue, 2)}</Box>
          </Box>
          <Box sx={itemStyle}>
            <Typography variant="div">P&L and earning rate</Typography>
            <div>
              <Box color={renderNumberColor(earning)}>
                {formatNumber(earning, 2)}
              </Box>
              <Box color={renderNumberColor(earningRate)}>
                {formatNumberByPercent(earningRate)}
              </Box>
            </div>
          </Box>
          <Box sx={itemStyle}>
            <Typography variant="div">Avg Price/Unit Value</Typography>
            <Stack direction={"row"} justifyContent={"center"}>
              <Box color={renderNumberColor(rtUnitvalue - averagePrice)}>
                {formatNumber(averagePrice, 4)}
              </Box>
              /
              <Box color={renderNumberColor(rtUnitvalue - averageUnitValue)}>
                {formatNumber(averageUnitValue, 4)}
              </Box>
            </Stack>
          </Box>
          <Box sx={{ ...itemStyle, bgcolor: "warning.main" }}>
            <Typography variant="div">Net AMT/Quota</Typography>
            <Box>
              {formatNumber(netAmt, 2)}/{formatNumber(netQuota)}
            </Box>
          </Box>
          <Box sx={itemStyle}>
            <Typography variant="div">Total Long AMT/Quota</Typography>
            <Box>
              {formatNumber(totalLongAmt, 2)}/{formatNumber(totallongQuota)}
            </Box>
          </Box>
          <Box sx={itemStyle}>
            <Typography variant="div">Total Short AMT/Quota</Typography>
            <Box>
              {formatNumber(totalShortAmt, 2)}/{formatNumber(totalShortQuota)}
            </Box>
          </Box>
          <Box
            sx={{
              ...itemStyle,
              flex: "4 4 600px",
            }}
          >
            <Typography variant="div">As of Toay</Typography>
            <Box
              sx={{
                display: "flex",
                gap: 1,
                flexWrap: "wrap",
                justifyContent: "center",
              }}
            >
              {Object.keys(estPriceTable || {}).map((key) => (
                <Box
                  key={key}
                  sx={{
                    padding: 1,
                    backgroundColor: (theme) =>
                      theme.palette.background.console,
                    borderRadius: 1,
                    boxShadow: "0 1px 3px rgba(0, 0, 0, 0.5)",
                    textAlign: "center",
                    flex: 1,
                  }}
                >
                    <Typography variant="div">{key * 100}%</Typography>
                    <Box>{formatNumber(estPriceTable[key], 4)}</Box>
                </Box>
              ))}
            </Box>
          </Box>
          <Box sx={itemStyle}>
            <Typography variant="div">Total Fee</Typography>
            <Box>{formatNumber(totalFee, 2)}</Box>
          </Box>
          <Box sx={itemStyle}>
            <Typography variant="div">{fxRateInfo.currency} / RMB</Typography>
            <Box>{fxRateInfo?.fxRate}</Box>
            <Box>{fxRateInfo?.asOfTime}</Box>
          </Box>
          <Box sx={itemStyle}>
            <Typography variant="div">Total Dividend AMT</Typography>
            <Box>{formatNumber(totalDividendAmt, 2)}</Box>
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
}

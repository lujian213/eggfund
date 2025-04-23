import { Button, Modal, Stack, Typography } from "@mui/material";
import axios from "axios";
import { useEffect, useState } from "react";
import HighchartsReact from 'highcharts-react-official';
import Highcharts from 'highcharts';
import { BASE_URL } from "../../../utils/get-baseurl";

const style = {
  position: "absolute",
  top: "50%",
  left: "50%",
  transform: "translate(-50%, -50%)",
  width: "90vw",
  boxshadow: 24,
  background: (theme) => theme.palette.background.sidebar,
  p: 4,
};

export default function RealtimeTrendChartModal(props) {
  const { open, data, handleClose } = props;
  const { code, fundName } = data || {};
  const [datasource, setDatasource] = useState([]);

  useEffect(() => {
    const fetchDatasource = async () => {
      const response = await axios.get(`${BASE_URL}/rtvalues/history`, {
        params: {
          code,
        },
      });
      const result = response.data || [];
      setDatasource(result);
    };
    code && fetchDatasource();
  }, [code]);

  const handleModalClose = () => {
    handleClose();
  };

  const options = {
    chart: {
      height: 500,
    },
    title: {
      text: "Real-Time Increase Rate Throughout the Day",
    },
    accessibility: {
      point: {
        valueDescriptionFormat: "{value}",
      },
    },
    xAxis: {
      title: {
        text: "Increase Rate",
      },
      categories: datasource.map((item) => item.time.split(" ")?.[1]),
    },
    yAxis: {
      title: {
        text: "INCREASE RATE %",
      },
    },
    tooltip: {
      headerFormat: "<b>{series.name}</b><br />",
      pointFormat:
        "{point.category}<br /> {point.y}%<br /> Unit Value: {point.unitValue}",
    },
    series: [
      {
        name: "INCREASE RATE %",
        data: datasource.map((item) => ({
          y: parseFloat((item.increaseRate * 100).toFixed(2)),
          unitValue: item.unitValue,
        })),
      },
    ],
    credits: {
      enabled: false,
    },
  };

  return (
    <Modal
      open={open}
      onClose={handleModalClose}
      aria-labelledby="modal-modal-title"
      aria-describedby="modal-modal-description"
    >
        <Stack sx={style} spacing={2}>
        <Typography id="confirm-modal-title" sx={{
            fontSize: '2rem'
        }}>
            {fundName}
            </Typography>
            <Stack spacing={2}>
                <HighchartsReact highcharts={Highcharts} options={options} />
            </Stack>
            <Stack direction={"row"} spacing={2} sx={{alignSelf: 'flex-end'}}>
                <Button onClick={handleModalClose}>Cancel</Button>
            </Stack>
        </Stack>
    </Modal>
  );
}

import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";
import Modal from "@mui/material/Modal";
import { Box, Stack } from "@mui/system";
import AggridWrapper from "../../../components/aggrid-wrapper";
import { useMemo, useState } from "react";
import { formatNumber } from "../../../utils/process-number";
import axios from "axios";
import { BASE_URL } from "../../../utils/get-baseurl";
import { useSetRecoilState } from "recoil";
import { refreshInvestsState } from "../invests-config/store/atom";
import { Switch } from "@mui/material";

const style = {
  position: "absolute",
  top: "50%",
  left: "50%",
  transform: "translate(-50%, -50%)",
  width: "min(1000px, 90%)",
  boxshadow: 24,
  background: (theme) => theme.palette.background.sidebar,
  p: 4,
};

export default function UploadPreviewModal(props) {
  const { open, handleClose, data } = props;
  const { investor, invests } = data || {};
  const [overwrite, setOverwrite] = useState(false);
  const refetchInvests = useSetRecoilState(refreshInvestsState);

  const [selectedRowIds, setSelectedRowIds] = useState([]);

  const colDefs = useMemo(
    () => [
      { field: "day", flex: 1, minWidth: 130 },
      { field: "code", flex: 1 },
      { field: "name", flex: 1, minWidth: 130 },
      { field: "share", flex: 1 },
      { field: "unitPrice", flex: 1 },
      { field: "type", flex: 1 },
      { field: "fee", flex: 1 },
      { field: "tax", flex: 1 },
      {
        field: "fxRate",
        flex: 1,
      },
      {
        field: "amount",
        headerName: "Effective Amt",
        flex: 1,
        valueGetter: (p) => formatNumber(p.data.amount),
      },
    ],
    []
  );

  const confirm = async () => {
    const selectedInvests = invests.filter((item) =>
      selectedRowIds.includes(`${item.code}-${item.day}-${item.type}-${item.unitPrice}-${item.share}`)
    );
    const response = await axios.put(
      `${BASE_URL}/invest/${investor.id}`,
      selectedInvests,
      {
        params: {
          overwrite: overwrite,
        },
      }
    );
    response?.data && handleModalClose();
    refetchInvests((pre) => pre + 1);
  };

  const handleModalClose = () => {
    handleClose();
  };

  const onSelectionChanged = (selectedRows) => {
    const selectedIds = selectedRows.api.getSelectedRows().map((row) => `${row.code}-${row.day}-${row.type}-${row.unitPrice}-${row.share}`);
    setSelectedRowIds(selectedIds);
  };

  return (
    <Modal
      open={open}
      onClose={handleModalClose}
      aria-labelledby="modal-modal-title"
      aria-describedby="modal-modal-description"
    >
      <Stack sx={style} spacing={2}>
        <Typography id="confirm-modal-title">
          Add Invests for {investor?.name}
        </Typography>
        <Stack spacing={2}>
          <Box sx={{ display: "flex", alignItems: "center" }}>
            <Switch
              checked={overwrite}
              onChange={(e) => setOverwrite(e.target.checked)}
              inputProps={{ "aria-label": "controlled" }}
              color="primary"
              size="small"
            />
            <Typography>Overwrite existing invests</Typography>
          </Box>
          <AggridWrapper
            rowData={invests}
            columnDefs={colDefs}
            rowSelection={{ mode: "multiRow", isRowSelectable: (rowNode) => rowNode.data ? !!rowNode.data.name : false }}
            onSelectionChanged={onSelectionChanged}
            getRowId={(params) => `${params.data.code}-${params.data.day}-${params.data.type}-${params.data.unitPrice}-${params.data.share}`}
          />
        </Stack>
        <Stack direction={"row"} spacing={2} sx={{ alignSelf: "flex-end" }}>
          <Button variant="contained" onClick={confirm} autoFocus>
            Confirm
          </Button>
          <Button onClick={handleModalClose}>Cancel</Button>
        </Stack>
      </Stack>
    </Modal>
  );
}

import { useState } from "react";
import { useSetRecoilState } from "recoil";
import { refreshSummaryState } from "../../../store/atom";
import { Box, Button, Modal, Stack } from "@mui/material";
import DetailsTable from "./table";

export default function Details() {
  const [openModal, setOpenModal] = useState(false);
  const refreshSummary = useSetRecoilState(refreshSummaryState);

  const handleModalOpen = () => {
    setOpenModal(true);
  };

  const handleModalClose = () => {
    setOpenModal(false);
    refreshSummary((pre) => pre + 1);
  };

  return (
    <Box
      style={{
        flexBasis: "100%",
      }}
    >
      <DetailsTable handleModalOpen={handleModalOpen} />
      <DetailsModal open={openModal} handleClose={handleModalClose} />
    </Box>
  );
}

const style = {
  position: "absolute",
  top: "50%",
  left: "50%",
  transform: "translate(-50%, -50%)",
  width: "90vw",
  height: "90vh",
  boxshadow: 24,
  background: (theme) => theme.palette.background.sidebar,
  p: 4,
};

function DetailsModal(props) {
  const {open, handleClose} = props;

  return (
    <Modal
      open={open}
      onClose={handleClose}
      aria-labelledby="modal-modal-title"
      aria-describedby="modal-modal-description"
    >
        <Stack sx={style} spacing={2}>
            <Box sx={{flex: 1}}>
                <DetailsTable />
            </Box>
            <Box sx={{alignSelf: 'flex-end'}}>
                <Button onClick={handleClose}>Close</Button>
            </Box>
        </Stack>
    </Modal>
  );
}

import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import Modal from "@mui/material/Modal";
import { useState } from "react";
import { IconButton, Stack } from "@mui/material";
import CustomAvatar, { icons } from "../../../utils/get-icons";

const style = {
  position: "absolute",
  top: "50%",
  left: "50%",
  transform: "translate (-50%, -50%)",
  width: 600,
  boxshadow: 24,
  background: (theme) => theme.palette.background.sidebar,
  p: 4,
};
export default function AvatarSelect(props) {
  const { iconIndex, changeAvatar } = props;
  const [open, setOpen] = useState(false);

  const handleClick = () => setOpen(true);

  const handleModalClose = () => setOpen(false);

  return (
    <Box>
      <IconButton aria-label="avatar" onClick={handleClick}>
        <CustomAvatar
          id={iconIndex || 0}
          style={{
            height: "72px",
            width: "72px",
          }}
        />
      </IconButton>
      <Typography variant="body2">Select Avatar</Typography>
      <Modal
        open={open}
        onClose={handleModalClose}
        aria-labelledby="modal-modal-title"
        aria-describedby="modal-modal-description"
      >
        <Stack sx={style} spacing={2}>
          <Typography id="confirm-modal-title" variant="h6" component="h2">
            Select Avatar
          </Typography>
          <Stack
            direction={"row"}
            spacing={0.5}
            alignItems={"center"}
            sx={{ flexWrap: "wrap" }}
          >
            {icons.map((icon, index) => {
              return (
                <IconButton
                  key={index}
                  size="small"
                  onClick={() => {
                    changeAvatar(index);
                    setOpen(false);
                  }}
                >
                  <CustomAvatar
                    id={index}
                    style={{
                      height: "48px",
                      width: "48px",
                    }}
                  />
                </IconButton>
              );
            })}
          </Stack>
        </Stack>
      </Modal>
    </Box>
  );
}

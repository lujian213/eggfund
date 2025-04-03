import React, { useState } from "react";
import { TextField, Button, Paper, Box, Typography } from "@mui/material";
import axios from "axios";
import { userInfoState } from "../../store/atom";
import { useSetRecoilState } from "recoil";

const BASE_URL = process.env.REACT_APP_BASE_URL;

export default function Login() {
  const setUserInfo = useSetRecoilState(userInfoState);
  const [form, setForm] = useState({
    username: "",
    password: "",
  });

  const handleLogin = async () => {
    const response = await axios.get(`${BASE_URL}/loginUser`, {
      auth: {
        username: form.username,
        password: form.password,
      },
    });
    const user = response.data;
    setUserInfo(user);
  };

  return (
    <Box
      sx={{
        height: "100vh",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        background: (theme) => theme.palette.background.report,
      }}
    >
      <Paper
        elevation={3}
        sx={{
          padding: 4,
          width: "min(80vw, 500px)",
          textAlign: "center",
        }}
      >
        <Typography variant="h5" component="h1" gutterBottom>
          Login
        </Typography>
        <Box
          component="form"
          sx={{
            display: "flex",
            flexDirection: "column",
            gap: 2,
          }}
        >
          <TextField
            value={form.username}
            onChange={(e) => setForm({ ...form, username: e.target.value })}
            label="Username"
            variant="outlined"
            fullWidth
            required
          />
          <TextField
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            label="Password"
            type="password"
            variant="outlined"
            fullWidth
            required
          />
          <Button
            variant="contained"
            color="primary"
            fullWidth
            onClick={handleLogin}
          >
            Login
          </Button>
        </Box>
      </Paper>
    </Box>
  );
}

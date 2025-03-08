import React, { Suspense } from "react";
import { createHashRouter } from "react-router-dom";
import App from "../App";

const Overview = React.lazy(() => import("../pages/overview"));
const RtValues = React.lazy(() => import("../pages/rt-values"));
const Settings = React.lazy(() => import("../pages/settings"));

export const router = createHashRouter([
  {
    path: "/",
    element: (
      <Suspense>
        <App />
      </Suspense>
    ),
    children: [
      {
        path: "",
        element: (
          <Suspense>
            <Overview />
          </Suspense>
        ),
      },
      {
        path: "overview",
        element: (
          <Suspense>
            <Overview />
          </Suspense>
        ),
      },
      {
        path: "rt-values",
        element: (
          <Suspense>
            <RtValues />
          </Suspense>
        ),
      },
      {
        path: "settings",
        element: (
          <Suspense>
            <Settings />
          </Suspense>
        ),
      },
      {
        path: "*",
        element: <div>Page Not Found</div>,
      },
    ],
  },
]);

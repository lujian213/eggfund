import { useRecoilValue } from "recoil";
import React from "react";
import { AgGridReact } from "ag-grid-react";
import { themeState } from "../../store/atom";

function AGGridWrapper(props, ref) {
  const { children, ...rest } = props;
  const theme = useRecoilValue(themeState);

  const themeType = theme === "Lightblue" || theme === "Pink" ? "" : "-dark";

  return (
    <div style={{ height: "100%", flex: 1, flexBasis: 500 }}>
      <div className={`ag-theme-alpine${themeType}`} style={{ height: "100%" }}>
        <AgGridReact enableCellTextSelection={true} ref={ref} {...rest} defaultColDef={{ minWidth: 100 }}>
          {children}
        </AgGridReact>
      </div>
    </div>
  );
}

export default React.forwardRef(AGGridWrapper);

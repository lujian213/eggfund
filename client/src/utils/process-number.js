export function roundDecimal(number, decimal = 2) {
  const factor = Math.pow(10, decimal);
  const roundedNumber = Math.round(number * factor) / factor;
  return roundedNumber.toFixed(decimal);
}

export function numberToPercent(number) {
  return number * 100;
}

export function zeroToDash(number) {
  return number === 0 ? "-" : number;
}

export function formatNumber(number, decimal = 2) {
  let rounded = roundDecimal(number, decimal);
  const value = new Intl.NumberFormat("en-US", {
    minimumFractionDigits: decimal,
    maximumFractionDigits: decimal,
  }).format(rounded);
  if (Number(value) === 0) return "-";
  if (value === "NaN") return "-";
  if (value.includes("-")) return `(${value.replace("-", "")})`;
  return value;
}

export function formatNumberByPercent(number, decimal = 2) {
  const percent = Number(number) * 100;
  let rounded = roundDecimal(percent, decimal);
  const value = new Intl.NumberFormat("en-US", {
    minimumFractionDigits: decimal,
    maximumFractionDigits: decimal,
  }).format(rounded);
  if (Number(value) === 0) return "-";
  if (value === "NaN") return "-";
  if (value.includes("-")) return `(${value.replace("-", "")}%)`;
  return `${value}%`;
}

import Icon1 from "../assets/vibrent_1.png";
import Icon2 from "../assets/vibrent_3.png";
import Icon3 from "../assets/vibrent_4.png";
import Icon4 from "../assets/vibrent_10.png";

export const icons = [Icon1, Icon2, Icon3, Icon4];

export default function CustomAvatar(props) {
  const { id, style } = props;
  const index = Number(id);
  const avatar = icons[index];

  return (
    <img src={avatar} style={{ borderRadius: "50%", ...style }} alt="avatar" />
  );
}

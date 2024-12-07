interface TextAlignProps {
  value: string | '-' | number;
  textAlign?: 'center' | 'left' | 'right' | undefined;
}

export const TextAlign = ({ value,textAlign='center' }: TextAlignProps) => {
  return <div style={{ textAlign }}>{value}</div>
}

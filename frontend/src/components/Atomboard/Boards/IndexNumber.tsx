import React from "react";

interface IndexNumberProps {
  value: number;
}

export const IndexNumber = ({ value }: IndexNumberProps) => {
  return <span style={{ fontWeight: 700 }}>{value.toString().padStart(2, '0')}</span>
}
// import { useState, useEffect } from 'react';
//
// export function useData<T>(url: string): T[] {
//   const [data, setData] = useState<T[]>([]);
//
//   useEffect(() => {
//     fetch(url)
//       .then(res => res.json())
//       .then(data => setData(data))
//   }, [url])
//
//   return data;
// }

import { useState, useEffect } from 'react';

export function useData(url: string): any[] {
  const [data, setData] = useState([]);

  useEffect(() => {
    fetch(url)
        .then(res => res.json())
        .then(data => setData(data))
  }, [url])

  return data;
}
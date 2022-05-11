import React from 'react'
import Skeleton from 'react-loading-skeleton'

import 'react-loading-skeleton/dist/skeleton.css'

interface SkeletonRowProps {
  rows: number
  columns: number
}

export const SkeletonRow: React.VFC<SkeletonRowProps> = ({ rows, columns }) => (
  <>
    {Array.from({ length: rows }).map((_, i) => <tr key={i}>
      {Array.from({ length: columns }).map((_, j) => <td key={j}><Skeleton /></td>)}
    </tr>)}
  </>
)

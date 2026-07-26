/* eslint-disable @eslint-react/no-array-index-key */

import 'react-loading-skeleton/dist/skeleton.css'

import React from 'react'
import Skeleton from 'react-loading-skeleton'

interface SkeletonRowProps {
  rows: number
  columns: number
}

export const SkeletonRow: React.FC<SkeletonRowProps> = ({ rows, columns }) => (
  <>
    {Array.from({ length: rows }).map((_, i) => (
      <tr key={i}>
        {Array.from({ length: columns }).map((_, j) => (
          <td key={j}>
            <Skeleton />
          </td>
        ))}
      </tr>
    ))}
  </>
)

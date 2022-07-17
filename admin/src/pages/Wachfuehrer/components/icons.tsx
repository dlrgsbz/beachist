import CheckBoxIcon from '@mui/icons-material/CheckBox'
import CloudOffOutlinedIcon from '@mui/icons-material/CloudOffOutlined'
import CloudOutlinedIcon from '@mui/icons-material/CloudOutlined'
import DisabledByDefaultIcon from '@mui/icons-material/DisabledByDefault'
import IndeterminateCheckBoxIcon from '@mui/icons-material/IndeterminateCheckBox'
import React from 'react'
import { SvgIconProps } from '@mui/material'

export const SquareX = () => <DisabledByDefaultIcon sx={{ color: 'red' }} />

export const SquareEmpty = () => <IndeterminateCheckBoxIcon />

export const SquareCheck = () => <CheckBoxIcon sx={{ color: 'green' }} />

export const ConnectedIcon = (props: SvgIconProps) => <CloudOutlinedIcon {...props} />

export const DisconnectedIcon = (props: SvgIconProps) => <CloudOffOutlinedIcon {...props} />

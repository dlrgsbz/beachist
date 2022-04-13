import React from 'react'
import CheckBoxIcon from '@mui/icons-material/CheckBox'
import IndeterminateCheckBoxIcon from '@mui/icons-material/IndeterminateCheckBox'
import DisabledByDefaultIcon from '@mui/icons-material/DisabledByDefault'
import CloudOutlinedIcon from '@mui/icons-material/CloudOutlined'
import CloudOffOutlinedIcon from '@mui/icons-material/CloudOffOutlined'
import { SvgIconProps } from '@mui/material'

export const SquareX = () => <DisabledByDefaultIcon />

export const SquareEmpty = () => <IndeterminateCheckBoxIcon />

export const SquareCheck = () => <CheckBoxIcon />

export const ConnectedIcon = (props: SvgIconProps) => <CloudOutlinedIcon {...props} />

export const DisconnectedIcon = (props: SvgIconProps) => <CloudOffOutlinedIcon {...props} />

export interface Shadow {
  state?: ShadowState | null
  version?: number | null
}

export interface ShadowState {
  reported?: ReportedShadowState | null
  desired?: DesiredShadowState | null
}

export interface ReportedShadowState extends DesiredShadowState {
  appVersion?: string | null
  appVersionCode?: number | null
  connected?: number | null
}

export interface DesiredShadowState {
  stationId?: string | null
}

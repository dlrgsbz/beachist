import 'react-loading-skeleton/dist/skeleton.css'

import { Option, RenderTokenProps } from 'react-bootstrap-typeahead/types/types'
import React, { useEffect, useState } from 'react'
import { Token, Typeahead } from 'react-bootstrap-typeahead'
import { useAdminFieldStore, useAdminStore } from 'store'

import { BaseStationInfo } from '../../../dtos'
import Skeleton from 'react-loading-skeleton'
import { useObserver } from 'mobx-react-lite'

interface StationSelectProps {
  value: BaseStationInfo[]
  onChange: (stations: BaseStationInfo[]) => void
}

const useStores = () => {
  const store = useAdminStore()

  const [stations, setStations] = useState<BaseStationInfo[]>([])

  const fetchedStations = useObserver(() => store.stationsState.data)

  useEffect(() => {
    setStations([allStations, ...fetchedStations])
  }, [fetchedStations])

  return useObserver(() => ({
    stations,
    stationsLoading: store.stationsState.status === 'pending',
    fetchStations: store.fetchBaseStationInfos,
  }))
}

const NULL_UUID = '00000000-0000-0000-0000-000000000000'

export const StationSelect: React.VFC<StationSelectProps> = ({ value, onChange }) => {
  const { stations, stationsLoading, fetchStations } = useStores()

  useEffect(() => {
    fetchStations()
  }, [fetchStations])

  const onStationChange = (selectedOptions: Option[]) => {
    const selected = mapOptionsToStations(selectedOptions)

    const added = selected.filter(x => !value.includes(x))

    if (added.find(({ id }) => id === NULL_UUID)) {
      onChange([stations[0]])
      return
    }

    if (value.find(({ id }) => id === NULL_UUID) && !added.find(({ id }) => id === NULL_UUID)) {
      onChange(selected.filter(({ id }) => id !== NULL_UUID))
      return
    }

    onChange(selected)
  }

  if (stationsLoading) {
    return <Skeleton />
  }

  return (
    <Typeahead
      options={stations}
      labelKey="name"
      id="station"
      placeholder="Station(en) auswählen"
      renderToken={renderToken}
      selected={value}
      onChange={items => onStationChange(items)}
      multiple
      clearButton
    />
  )
}

export const allStations: BaseStationInfo = { id: NULL_UUID, name: 'Alle Stationen', hasSearch: false }

const mapOptionsToStations = (items: unknown[]): BaseStationInfo[] => {
  if (!items.every(isStation)) {
    return []
  }

  return items
}

const isStation = (item: unknown): item is BaseStationInfo => {
  return (typeof item === 'object' && item?.hasOwnProperty('name')) || false
}

const renderToken = (option: Option, { onRemove }: RenderTokenProps, idx: number) => {
  if (!isStation(option)) {
    return <></>
  }

  return (
    <Token key={idx} onRemove={onRemove} option={option}>
      {option.name}
    </Token>
  )
}

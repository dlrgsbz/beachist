import { observable, reaction } from 'mobx'
import moment, { Moment } from 'moment'

class NavigationStore {
  @observable currentWachtag: Moment = moment().startOf('day')

  constructor() {
    reaction(
      () =>
        moment()
          .startOf('minute')
          .format(),
      (val, r) => {
        console.log(val)
      },
      {
        fireImmediately: true,
        equals: (a, b) => a === b,
      },
    )
  }
}

export default NavigationStore

import * as React from 'react'

const FeldRow: React.FC<{ uuid: string }> = ({ uuid }) => {
  // const { field, onFeldChange, setFeldRequired, onCommentChange } = {...}

  return <tr key={uuid} />

  // return <tr>
  //   <td><input type="text" value={field.name} onChange={e => onFeldChange(e.target.value)}/></td>
  //   <td>
  //     <input type="checkbox" checked={(typeof field.required === 'undefined')}
  //            onChange={event => setFeldRequired((typeof field.required === 'undefined') ? 1 : undefined)}/>&nbsp; Nur
  //     Überschrift <br/>
  //     <input type="number" disabled={(typeof field.required === 'undefined')}
  //            onChange={event => setFeldRequired(parseInt(event.target.value, 10))} value={field.required}/>
  //   </td>
  //   <td><input type="text" value={field.note} onChange={event => onCommentChange(event.target.value)}/></td>
  //   <td>
  //     <input type='checkbox' id='alle'/><label htmlFor='alle'>alle Stationen</label>
  //     <select multiple={true}>
  //       {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 'Offendorf'].map(station =>
  //         <option>Station {station}</option>)}
  //     </select>
  //   </td>
  // </tr>
}

type AdminProps = {
  felder: string[]
}

const Admin: React.FC<AdminProps> = ({ felder }) => {
  return (
    <div>
      <h1>Admin</h1>
      <table className="table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Anzahl</th>
            <th>Bemerkung</th>
          </tr>
        </thead>
        <tbody>
          {felder.map(uuid => (
            <FeldRow key={uuid} uuid={uuid} />
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default Admin

import { cli } from './cli'
export class Message {
  static fromJSON (buffer) {
    return new Message(JSON.parse(buffer.toString()))
  }

  constructor ({ username, command, contents }) {
    this.username = username
    this.command = command
    this.contents = contents
  }

  toJSON () {
    return JSON.stringify({
      username: this.username,
      command: this.command,
      contents: this.contents
    })
  }

  toString () {
    if (this.command === 'connect') {
      return cli.chalk['green'](`${this.contents}`)
    } else if (this.command === 'disconnect') {
      return cli.chalk['red'](`${this.contents}`)
    } else if (this.command === 'echo') {
      return cli.chalk['blue'](`${this.contents}`)
    } else if (this.command === 'broadcast') {
      return cli.chalk['white'](`${this.contents}`)
      // return cli.chalk['grey'](`${this.timeStamp} <${this.username}> (all): ${this.contents}`)
    } else if (this.command === 'users') {
      return cli.chalk['yellow'](`${this.contents}`)
    } else {
      return cli.chalk['grey'](`${this.contents}`)
    }
  }
}

import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server
let clientPort = null
let host = null
// let comDelim = [ , cli.chalk['red']('users>'), cli.chalk['blue']('broadcast>')]

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username> ["host"], [port]')
  .delimiter(cli.chalk['green']('connected>'))
  .init(function (args, callback) {
    username = args.username
    if (host == null || clientPort == null) {
      host = 'localhost'
      clientPort = 8080
    }
    server = connect({ host: host, port: clientPort }, () => {
      server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
      callback()
    })

    server.on('data', (buffer) => {
      this.log(Message.fromJSON(buffer).toString())
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    const [ command, ...rest ] = words(input) //, /[^, ] + /g)
    const contents = rest.join(' ')

    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
    } else if (command === 'echo') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command === 'broadcast') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command === 'users') {
      // cli.ui.delimiter(cli.chalk['red'](contents))
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command === '@') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else {
      this.log(`Command <${command}> was not recognized`)
    }

    callback()
  })

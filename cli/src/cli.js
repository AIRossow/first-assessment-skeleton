import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server
let clientPort = null
let host = null
// let lastCommand = ''
// let lastCont = ''
// let timeStamp = String(Date.now)

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
    // if (input.keyCode === 13) {
    //   this.log('You must enter a command.')
    //   callback()
    // }
    const [ command, ...rest ] = words(input)//, /[^, ] + /g)
    const contents = rest.join(' ')
    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
    } else if (command === 'echo') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command === 'broadcast') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command === 'users') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (input[0] === '@') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command === 'help') {
      this.log('Command list:')
      this.log('broadcast (message)   //broadcasts message to all users')
      this.log('disconnect            //disconnect from server')
      this.log('echo (message)        //repeats message back to you')
      this.log('users                 //lists all connected users')
      this.log('@<username> (message) //message can only be seen by specified username')
    } else {
      this.log(`Command <${command}> was not recognized. Enter help for valid commands`)
    }

    callback()
  })

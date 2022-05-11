import winston, { transports } from 'winston'

const { LOG_LEVEL = 'info' } = process.env;

export const logger = winston.createLogger({
    level: LOG_LEVEL,
    format: winston.format.json(),
    transports: [
        new transports.Console(),
    ],
});

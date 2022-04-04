export const requireEnv = (name: string): string => {
    const value = process.env[name];
    if (value === undefined) {
        throw new Error(`Missing env: ${name}`);
    }
    return value;
};

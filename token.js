const { SkyWayAuthToken, uuidV4 } = require('@skyway-sdk/token');
const token = new SkyWayAuthToken({
  jti: uuidV4(),
  iat: Math.floor(Date.now() / 1000),
  exp: Math.floor(Date.now() / 1000) + 60 * 60 * 24,
  scope: {
    app: {
      id: '2ed0ad63-8188-47e5-9cfa-a7b7aed86f96',
      turn: true,
      actions: ['read'],
      channels: [
        {
          id: '*',
          name: '*',
          actions: ['write'],
          members: [
            {
              id: '*',
              name: '*',
              actions: ['write'],
              publication: {
                actions: ['write'],
              },
              subscription: {
                actions: ['write'],
              },
            },
          ],
          sfuBots: [
            {
              actions: ['write'],
              forwardings: [
                {
                  actions: ['write'],
                },
              ],
            },
          ],
        },
      ],
    },
  },
}).encode('5oDQAcbZjRv7FchrH5/Cz2egJaYbCZronI2luPN0hGM=');
console.log(token);

# membot ![release](https://img.shields.io/badge/version-0.1-orange.svg)

**A java memory web shell extracting tool**

- What:

  侵入式，目前的目标是能够从内存中筛选出已知内存马植入软件的恶意代码。

- Usage:

  ```
  java -jar membot.jar
  ```

- Cover

| 类型 | 是否覆盖 | 说明               |
|----|------|------------------|
| 冰蝎 |   ✔   | 支持Agent和非Agent方式 |
| 哥斯拉 |   ✔   |                  |
| 蚁剑 |   ✔   |                  |
| websocket |   ✔   |                  |
| ReGeorg |   ✔   |                  |
| NeoreGeorg |   ✔   |                  |
| UpgradeMemshell |   ✔   |                  |
| Suo5 |   ✔   |                  |
| BastionFilter |   ✔   |                  |


- Reference:

  [copagent](https://github.com/LandGrey/copagent)

  [arthas](https://github.com/alibaba/arthas)
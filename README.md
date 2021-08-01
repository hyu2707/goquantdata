[![codecov](https://codecov.io/gh/hyu2707/goquant/branch/master/graph/badge.svg)](https://codecov.io/gh/hyu2707/goquant)

# goquant offline quant system
Python based offline quant system in AWS EC2. Mainly used for:
- fetch and build historical market data database in s3
- logging readltime streaming data to s3, such as orderbook (Airflow + Kafka)
- offline worklfow pipeline for long running ML job (Airflow)
- offline algorithm research

For realtime online trading and backtesting system, please see goquant online quant system

Key Tech Stacks:
- Airflow: workflow orchestration
- Kafka: msg queue, data communication
- S3: cold data storage
- EC2: service host
- CloudWatch: monitoring and alerting

## Current Status

```diff
+ [data] US stock: Polygon, minute-level, 2015 to now
+ [data] crypto: Binance, minute-level, last 3 month
```

## Use Guide
Step 1. Create private config in `config/priv.yaml`
register free account in Alpaca
follow this format:
```
alpaca:
  id: ""
  key: ""
binance:
  key: ""
  secret: ""
aws:
  id: ""
  key: ""
bitmex:
  id: ""
  key: ""
service:
  airflow:
    email: ["your email"]
```
Step 2. Build env
```
make install
```
then activate env:
```bash
source env/bin/activate
```
Step 3. Check tests

unittest (fast)
```bash
make test
```
full test (slow), it contains integration test, run it before submit code
```bash
make test_all
```
Step 4. Start workflow
```bash
make run
```

## Dev Guide: Ubuntu - AWS EC2 Ubuntu 18
1. Init instance
```bash
sudo apt update
sudo apt-get install libmysqlclient-dev
sudo apt-get install libssl-dev
sudo apt install make
sudo apt install python3
sudo apt install python3-pip
```

2. Create project python env 
```bash
make install
source env/bin/activate
```

3. Create private config in `config/priv.yaml`. Register account at different websites.
follow this format:
```yaml
alpaca:
  id: ""
  key: ""
polygon:
  key: ""
binance:
  key: ""
  secret: ""
aws:
  id: ""
  key: ""
bitmex:
  id: ""
  key: ""
service:
  airflow:
    email: ["your email"]
```

4. Install and config mysql
```bash
sudo apt install mysql-server
sudo mysql
```
then in mysql cmd:
```bash
CREATE DATABASE airflow;
CREATE USER 'airflow'@'localhost' IDENTIFIED BY 'airflow';
GRANT ALL PRIVILEGES ON airflow. * TO 'airflow'@'localhost';
SET GLOBAL explicit_defaults_for_timestamp = 1;
```
start mysql
```bash
sudo /etc/init.d/mysql start
```

5. Config airflow
create default config:
```bash
make airflow
make airflow-stop
```
modify `airflow/airflow.cfg`, change those lines:
```bash
executor = LocalExecutor
sql_alchemy_conn = mysql://airflow:airflow@localhost:3306/airflow
```

6. Install Kafka
```bash
sudo apt install default-jre
wget http://ftp.wayne.edu/apache/kafka/2.4.0/kafka_2.12-2.4.0.tgz -O kafka.tgz 
mv kafka_2.12-2.4.0 kafka
```
start kafka
```bash
setsid nohup ./kafka/bin/zookeeper-server-start.sh kafka/config/zookeeper.properties &
setsid nohup ./kafka/bin/kafka-server-start.sh kafka/config/server.properties &
```
create kafka topics:
```bash
./kafka/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic bitmex_orderbook
```

7. Run!
```bash
setsid nohup make airflow &
```

8. Setup Cloudwatch
add this in crontab (make sure instance already have couldwatch IAM, see guide [here](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/mon-scripts.html)):
```
crontab -e
*/1 * * * * ~/aws-scripts-mon/mon-put-instance-data.pl --mem-used-incl-cache-buff --mem-util --disk-space-util --disk-path=/ --from-cron
```

## Runbook
1. New instance from existing AMI
```bash
source env/bin/activate
sudo /etc/init.d/mysql start
echo "" > nohup.out
setsid nohup ./kafka/bin/zookeeper-server-start.sh kafka/config/zookeeper.properties &
setsid nohup ./kafka/bin/kafka-server-start.sh kafka/config/server.properties &
./kafka/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic bitmex_orderbook
setsid nohup make airflow &
crontab -e
*/1 * * * * ~/aws-scripts-mon/mon-put-instance-data.pl --mem-used-incl-cache-buff --mem-util --disk-space-util --disk-path=/ --from-cron
```

## Dev Guide
### Test
```bash
make test
```
### Lint
```bash
make lint
```

### Jupyter Notebook env
```bash
make research
```


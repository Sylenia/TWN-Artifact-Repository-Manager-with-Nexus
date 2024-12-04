# README: Setting up Nexus Repository and Deploying Maven Artifacts

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Setting Up the Droplet](#setting-up-the-droplet)
3. [Installing and Configuring Nexus](#installing-and-configuring-nexus)
4. [Creating a Nexus User for Deployment](#creating-a-nexus-user-for-deployment)
5. [Configuring Nexus via the UI](#configuring-nexus-via-the-ui)
6. [Configuring Maven for Deployment](#configuring-maven-for-deployment)
7. [Deploying the Maven Artifact](#deploying-the-maven-artifact)

---

## Prerequisites

1. A DigitalOcean droplet (or any server) running Ubuntu 20.04 or later.
2. SSH access to the server.
3. Maven installed on your local development machine.
4. An application packaged as a JAR or other Maven-supported artifact.

---

## Setting Up the Droplet

1. Log in to the droplet via SSH:
   ```bash
   ssh root@<droplet-ip>
   ```

2. Update the system:
   ```bash
   apt update && apt upgrade -y
   ```

3. Install Java (required for Nexus):
   ```bash
   apt install openjdk-11-jdk -y
   ```

4. Verify the Java installation:
   ```bash
   java -version
   ```

---

## Installing and Configuring Nexus

1. Download Nexus OSS:
   ```bash
   wget https://download.sonatype.com/nexus/3/latest-unix.tar.gz
   ```

2. Extract the Nexus package:
   ```bash
   tar -xvzf latest-unix.tar.gz
   mv nexus-* /opt/nexus
   ```

3. Create a nexus user:
   ```bash
   useradd -M -d /opt/nexus -s /bin/bash nexus
   chown -R nexus:nexus /opt/nexus
   chmod -R 775 /opt/nexus
   ```

4. Make Nexus run as a service:
   ```bash
   nano /etc/systemd/system/nexus.service
   ```

   Add the following content:
   ```
   [Unit]
   Description=nexus service
   After=network.target

   [Service]
   Type=forking
   LimitNOFILE=65536
   ExecStart=/opt/nexus/bin/nexus start
   ExecStop=/opt/nexus/bin/nexus stop
   User=nexus
   Restart=on-abort

   [Install]
   WantedBy=multi-user.target
   ```

5. Reload and start the Nexus service:
   ```bash
   systemctl daemon-reload
   systemctl enable nexus
   systemctl start nexus
   ```

6. Check the Nexus service status:
   ```bash
   systemctl status nexus
   ```

7. Access Nexus in your browser at `http://<droplet-ip>:8081`.

---

## Creating a Nexus User for Deployment

1. Log in to Nexus with the default credentials:
   - Username: `admin`
   - Password: Found in `/opt/sonatype-work/nexus3/admin.password`.

   ```bash
   cat /opt/sonatype-work/nexus3/admin.password
   ```

2. Create a new user:
   - Navigate to **Settings > Security > Users**.
   - Click **Create user** and configure the following:
     - Username: `example-user`
     - Password: `example-password`
     - Role: Assign `nx-deploy` or a similar role with deployment privileges.

---

## Configuring Nexus via the UI

1. Create a Maven Repository:
   - Navigate to **Repositories** under **Settings**.
   - Click **Create repository** and select **maven2-hosted**.
   - Configure:
     - Name: `maven-releases` for release artifacts.
     - Version Policy: **Release**.
     - Deployment Policy: **Allow redeploy**.

2. Repeat the steps to create a repository for snapshots:
   - Name: `maven-snapshots`.
   - Version Policy: **Snapshot**.

---

## Configuring Maven for Deployment

1. Open or create the Maven settings.xml file:
   ```bash
   nano ~/.m2/settings.xml
   ```

2. Add the following configuration:
   ```xml
   <settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 http://maven.apache.org/xsd/settings-1.2.0.xsd">
       <servers>
           <server>
               <id>nexus-releases</id>
               <username>demo-user</username>
               <password>password</password>
           </server>
           <server>
               <id>nexus-snapshots</id>
               <username>demo-user</username>
               <password>password</password>
           </server>
       </servers>
   </settings>
   ```

---

## Deploying the Maven Artifact

1. Update the pom.xml of your project with distributionManagement:
   ```xml
   <distributionManagement>
       <repository>
           <id>nexus-releases</id>
           <url>http://<droplet-ip>:8081/repository/maven-releases/</url>
       </repository>
       <snapshotRepository>
           <id>nexus-snapshots</id>
           <url>http://<droplet-ip>:8081/repository/maven-snapshots/</url>
       </snapshotRepository>
   </distributionManagement>
   ```

2. Deploy the artifact:

   - For deploying the artifact, you can use one of the two approaches based on your setup or requirements:

   - Using the Explicit Deployment Command:

   - The -DaltDeploymentRepository parameter is used to explicitly specify the repository URL and type (default) when:

    **You want to temporarily override the repository configuration without modifying the pom.xml.**
    **You are testing or deploying to an alternative repository for a one-off deployment.**
    **The <distributionManagement> section in the pom.xml is incomplete or absent.**

   For a snapshot artifact:
   ```bash
   mvn deploy -DaltDeploymentRepository=nexus-snapshots::default::http://<droplet-ip>:8081/repository/maven-snapshots/
   ```

   For a release artifact:
   ```bash
   mvn deploy -DaltDeploymentRepository=nexus-releases::default::http://<droplet-ip>:8081/repository/maven-releases/
   ```

   Using the Default Maven Command:
   ```bash
   mvn deploy
   ```

3. Verify the deployment:
   - Go to Nexus UI, and check the respective repository for the uploaded artifact.
# LeetChain

## Overview

LeetChain is an experimental blockchain implementation where transactions consist of LeetCode questions and their corresponding answers. This project aims to explore blockchain technology while leveraging Elliptic Curve (EC) cryptography for wallet management and utilizing RocksDB for data persistence. Multi-threading is employed to enable concurrent operation of nodes, mining, and wallet management.

## Motivation

The primary goals of LeetChain are:

- **Understanding Blockchain**: Gain a deeper understanding of blockchain technology by implementing a simplified version.
- **Hands-On Learning**: Apply theoretical blockchain knowledge to a practical project involving real-world(ish) data and cryptography.
- **Educational Purpose**: Create a learning tool that combines blockchain concepts with coding challenges from LeetCode, using EC keys for secure wallet management and ROCKSDB for efficient data storage.

## Features

- **java.com.LeetChain.Block Structure**: Each block contains a set of transactions (LeetCode questions and answers).
- **Proof of Work**: Implement a basic Proof of Work (PoW) algorithm for block validation.
- **Decentralized java.com.LeetChain.Ledger**: Simulate a decentralized ledger where each node can add new blocks to the chain.
- **EC Keys for Wallets**: Utilize Elliptic Curve (EC) cryptography for secure wallet management, allowing users to sign transactions with their private keys.
- **RocksDB Persistence**: Use RocksDB for efficient and reliable data persistence, ensuring the integrity and accessibility of the blockchain data.
- **Multi-Threading**: Implement multi-threading to concurrently handle node operations, mining processes, and wallet management.

## Multi-Threading Details

Multi-threading is integrated to enhance the functionality and efficiency of LeetChain, allowing multiple processes to operate simultaneously. Key aspects include:

- **java.com.LeetChain.Node Operation**: A dedicated thread manages the node's operations, including network communication, receiving and broadcasting blocks and transactions, and maintaining the decentralized ledger.
- **Mining**: Another thread is responsible for the mining process. This includes solving the Proof of Work (PoW) algorithm to find valid blocks and add them to the blockchain.
- **java.com.LeetChain.Wallet Management**: A separate thread handles wallet operations, such as managing keys, signing transactions, and ensuring secure access to wallet functionalities.

By employing multi-threading, LeetChain achieves:

- **Improved Performance**: Simultaneous processing of node activities, mining, and wallet operations ensures that the blockchain network remains responsive and efficient.
- **Enhanced Scalability**: With independent threads for different tasks, the system can better handle increased loads and scale as needed..

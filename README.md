# DEAPI

A simple desktop utility for decrypting EAPI responses, the protocol used by NCM, built with Compose for Desktop.

This was mostly a fun project to learn more about Kotlin Multiplatform and Compose. The code is kept simple on purpose.

[Screenshot](docs/screenshot.png)

### Features

- Decrypts standard EAPI payloads.
- Handles newer, Gzip-compressed EAPI payloads.
- Simple hex string input.
- Drag & drop support for binary files.

### How to Use

1. Run the application.
2. Paste the ciphertext as a hex string, or drag & drop the raw response file onto the window.
3. Check the **"Use Gzip Decompression"** box for newer API responses, which are typically identified by an `x-aeapi` request header.
4. Click "Decrypt".

### Disclaimer

This tool is provided for personal educational and research purposes only. By using this software, you agree that you are responsible for complying with the terms of service of any application you choose to analyze.

### Roadmap

-   **v1.2.0:** Refactor the project to the modern KMP template.
-   **v2.0.0:** Introduce an Android target.
# Use a slim Python base image
FROM python:3.10-slim

# Set working directory inside the container
WORKDIR /chess-preprocessor

# Install system-level dependencies (e.g., gcc for some PyPi libs)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy the requirements first and install
COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

# Now copy the full project structure (config, services, utils, main.py, etc.)
COPY . .

# Set the default entrypoint to run your script
ENTRYPOINT ["python", "main.py"]

# Leave CMD empty so args can be passed during runtime
CMD []
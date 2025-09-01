# Use a smaller base image for the final stage
FROM python:3.10-slim

RUN apt-get update && apt-get install -y \
    ffmpeg \
    gcc \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

RUN pip install TTS

CMD ["tts", "--model_name", "tts_models/de/thorsten/tacotron2-DDC", "--text", "Hallo, das ist ein Test."]

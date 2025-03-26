# Stage 1: Build the Vite React app
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm install

# Copy source code
COPY . .

# Set environment variable at build time (will be overridden by docker-compose)
ARG VITE_WEBFLUX_BACKEND_URL
ENV VITE_WEBFLUX_BACKEND_URL=$VITE_WEBFLUX_BACKEND_URL

# Build the static site with Vite (uses env var inside .env or passed via ARG)
RUN npm run build


# Stage 2: Serve it with Nginx
FROM nginx:alpine

# Copy static site from builder
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
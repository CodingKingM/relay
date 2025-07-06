FROM node:23-alpine AS build

ARG API_URL
ENV API_URL=${API_URL}

WORKDIR /usr/src/app

COPY package*.json ./
RUN npm ci

COPY ./ ./

RUN echo "VITE_API_URL=${API_URL}" > .env
RUN npm run build

FROM nginx:stable-alpine
COPY --from=build /usr/src/app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
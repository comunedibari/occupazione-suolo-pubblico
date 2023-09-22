FROM node:12.16.1-alpine As builder

### STAGE 1: Build ###
WORKDIR /usr/src/app
COPY package.json package-lock.json ./

RUN npm install

COPY . .

ARG PROFILE
ENV PROFILE $PROFILE

RUN echo "Environment: ${PROFILE}"
RUN npm run build-${PROFILE}

### STAGE 2: Run ###
FROM nginx:1.15.8-alpine

COPY --from=builder /usr/src/app/dist/osp-fe/ /usr/share/nginx/html
COPY --from=builder /usr/src/app/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
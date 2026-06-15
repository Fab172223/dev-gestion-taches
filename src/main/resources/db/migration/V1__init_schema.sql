CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       last_name VARCHAR(100),
                       first_name VARCHAR(100),
                       email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE projects (
                          id BIGSERIAL PRIMARY KEY,
                          title VARCHAR(150),
                          description TEXT,
                          start_date DATE,
                          end_date DATE
);

CREATE TABLE tasks (
                       id BIGSERIAL PRIMARY KEY,
                       title VARCHAR(150),
                       description TEXT,
                       created_at TIMESTAMP,
                       due_date DATE,
                       status VARCHAR(50),
                       priority VARCHAR(50),
                       user_id BIGINT,
                       project_id BIGINT,

                       CONSTRAINT fk_tasks_user
                           FOREIGN KEY (user_id)
                               REFERENCES users(id),

                       CONSTRAINT fk_tasks_project
                           FOREIGN KEY (project_id)
                               REFERENCES projects(id)
);
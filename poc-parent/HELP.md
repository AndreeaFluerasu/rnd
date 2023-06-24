DB commands:
CREATE ROLE poc_parent WITH LOGIN PASSWORD 'poc_parent';
CREATE DATABASE poc_parent OWNER poc_parent;

\connect poc_parent poc_parent
CREATE SCHEMA db_locking;
CREATE SCHEMA multi_threading;
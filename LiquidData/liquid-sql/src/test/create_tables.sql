begin;

drop table student_registered_modules cascade;
drop table student cascade;
drop table module cascade;
drop table faculty cascade;

create table faculty 
(
    faculty_id int not null unique generated always as identity (start with 1, increment by 1),
    primary key (faculty_id),
    faculty_name varchar(64) not null unique
);

insert into faculty(faculty_name)
values('FACULTY OF SCIENCE');

create table module 
(
    module_id int not null unique generated always as identity (start with 1, increment by 1),
    primary key (module_id),
    module_code varchar(16) not null unique,
    module_name varchar(124) not null unique
);

insert into module(module_code, module_name)
values('CSC1A10', 'Computer Science 1A10');

insert into module(module_code, module_name)
values('IFM1A10', 'Informatics 1A10');

create table student
(
    student_number int not null unique,
    
);

commit;

-- Enable UUID extension
create extension if not exists "uuid-ossp";

-- PROFILES (Public user data)
create table profiles (
  id uuid references auth.users on delete cascade not null primary key,
  email text,
  full_name text,
  avatar_url text,
  role text check (role in ('patient', 'caregiver')) default 'patient',
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- MEDICINES
DROP TABLE IF EXISTS medicines CASCADE;

create table medicines (
  id uuid default uuid_generate_v4() primary key,
  user_id uuid not null, -- Removed FK reference, RLS handles security
  name text not null,
  dosage text, -- e.g., "500mg"
  stock integer default 0,
  unit text, -- e.g., "pills", "ml"
  instructions text,
  reminder_time text, -- e.g., "08:00"
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- RLS Policies
alter table profiles enable row level security;
alter table medicines enable row level security;

create policy "Users can view own profile" on profiles for select using (auth.uid() = id);
create policy "Users can update own profile" on profiles for update using (auth.uid() = id);

create policy "Users can view own medicines" on medicines for select using (auth.uid() = user_id);
create policy "Users can insert own medicines" on medicines for insert with check (auth.uid() = user_id);
create policy "Users can update own medicines" on medicines for update using (auth.uid() = user_id);
create policy "Users can delete own medicines" on medicines for delete using (auth.uid() = user_id);
